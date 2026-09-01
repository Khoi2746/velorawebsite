package com.velora.website.Service;

import com.velora.website.Entity.BaoHanh;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.BaoHanhRepository;
import com.velora.website.Repository.NguoiDungRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BaoHanhService {

    private static final Logger log = LoggerFactory.getLogger(BaoHanhService.class);

    // ==========================================================
    // 8 TRẠNG THÁI THỐNG NHẤT
    // ==========================================================
    public enum TrangThai {
        CHO_XU_LY,          // Mới gửi
        DA_DE_XUAT_LICH,    // Admin đã đề xuất lịch hẹn, chờ khách xác nhận
        DA_TIEP_NHAN,       // Khách đã xác nhận lịch hẹn
        YEU_CAU_DOI_LICH,   // Khách yêu cầu đổi giờ, chờ admin đề xuất lại
        DANG_SUA_CHUA,      // Đang xử lý kỹ thuật
        HOAN_TAT,           // Hoàn tất
        DA_HUY,             // Khách tự hủy
        TU_CHOI             // Admin từ chối yêu cầu
    }

    // Admin chuyển trạng thái qua endpoint /status — KHÔNG cho phép admin tự set DA_TIEP_NHAN
    // (trạng thái này chỉ được set qua hành động xác nhận của khách - confirmSchedule())
    private static final Map<TrangThai, Set<TrangThai>> ADMIN_TRANSITIONS = Map.of(
            TrangThai.CHO_XU_LY, EnumSet.of(TrangThai.DA_DE_XUAT_LICH, TrangThai.TU_CHOI),
            TrangThai.DA_DE_XUAT_LICH, EnumSet.of(TrangThai.TU_CHOI),
            TrangThai.YEU_CAU_DOI_LICH, EnumSet.of(TrangThai.DA_DE_XUAT_LICH, TrangThai.TU_CHOI),
            TrangThai.DA_TIEP_NHAN, EnumSet.of(TrangThai.DANG_SUA_CHUA),
            TrangThai.DANG_SUA_CHUA, EnumSet.of(TrangThai.HOAN_TAT),
            TrangThai.HOAN_TAT, EnumSet.noneOf(TrangThai.class),
            TrangThai.DA_HUY, EnumSet.noneOf(TrangThai.class),
            TrangThai.TU_CHOI, EnumSet.noneOf(TrangThai.class)
    );

    private static final Set<TrangThai> HUY_DUOC_TU = EnumSet.of(TrangThai.CHO_XU_LY);
    private static final Set<TrangThai> CONFIRM_DUOC_TU = EnumSet.of(TrangThai.DA_DE_XUAT_LICH);
    private static final Set<TrangThai> RESCHEDULE_DUOC_TU = EnumSet.of(TrangThai.DA_DE_XUAT_LICH);

    private final BaoHanhRepository repo;
    private final NguoiDungRepository nguoiDungRepository;
    private final JavaMailSender mailSender;

    public BaoHanhService(BaoHanhRepository repo,
                           NguoiDungRepository nguoiDungRepository,
                           JavaMailSender mailSender) {
        this.repo = repo;
        this.nguoiDungRepository = nguoiDungRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public BaoHanh saveRequest(BaoHanh baoHanh) {
        baoHanh.setTrangThai(TrangThai.CHO_XU_LY.name());
        BaoHanh saved = repo.save(baoHanh);
        log.info("Đã tạo mới yêu cầu bảo hành ID: {} cho người dùng ID: {}", saved.getMaBaoHanh(), saved.getMaNguoiDung());
        return saved;
    }

    public List<BaoHanh> getAllRequests() {
        return repo.findAll();
    }

    public List<BaoHanh> getPendingRequests() {
        return repo.findByTrangThai(TrangThai.CHO_XU_LY.name());
    }

    public List<BaoHanh> findByMaNguoiDung(Integer maNguoiDung) {
        return repo.findByMaNguoiDungOrderByNgayGuiDesc(maNguoiDung);
    }

    // ==========================================================
    // ADMIN: chuyển trạng thái (endpoint /status)
    // ==========================================================
    @Transactional
    public BaoHanh updateStatus(Integer id, String trangThaiMoi, String thoiGianHen) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu bảo hành với ID: " + id));

        TrangThai current = parseTrangThai(bh.getTrangThai());
        TrangThai target = parseTrangThai(trangThaiMoi);

        Set<TrangThai> allowed = ADMIN_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(TrangThai.class));
        if (!allowed.contains(target)) {
            throw new IllegalStateException(
                    "Không thể chuyển trạng thái từ " + current + " sang " + target + ".");
        }

        if (target == TrangThai.DA_DE_XUAT_LICH && (thoiGianHen == null || thoiGianHen.isBlank())) {
            throw new IllegalArgumentException("Vui lòng cung cấp thời gian hẹn khi đề xuất lịch cho khách.");
        }

        bh.setTrangThai(target.name());
        if (thoiGianHen != null && !thoiGianHen.isBlank()) {
            bh.setThoiGianHen(thoiGianHen);
            bh.setThoiGianKhachMongMuon(null); // đã đề xuất lịch mới -> xóa yêu cầu đổi giờ cũ
        }

        BaoHanh updated = repo.save(bh);
        log.info("Cập nhật trạng thái yêu cầu bảo hành ID: {} từ {} -> {}", id, current, target);

        if (target == TrangThai.DA_DE_XUAT_LICH && updated.getMaNguoiDung() != null) {
            sendEmailToCustomerAsync(updated);
        }

        return updated;
    }

    // ==========================================================
    // USER: hủy yêu cầu (chỉ khi CHO_XU_LY)
    // ==========================================================
    @Transactional
    public BaoHanh cancelRequest(Integer id) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu bảo hành với ID: " + id));

        TrangThai current = parseTrangThai(bh.getTrangThai());
        if (!HUY_DUOC_TU.contains(current)) {
            throw new IllegalStateException("Chỉ có thể hủy yêu cầu đang ở trạng thái chờ xử lý.");
        }

        bh.setTrangThai(TrangThai.DA_HUY.name());
        BaoHanh cancelled = repo.save(bh);
        log.info("Đã hủy yêu cầu bảo hành ID: {}", id);
        return cancelled;
    }

    // ==========================================================
    // USER: xác nhận lịch hẹn do admin đề xuất
    // ==========================================================
    @Transactional
    public BaoHanh confirmSchedule(Integer id) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu bảo hành ID: " + id));

        TrangThai current = parseTrangThai(bh.getTrangThai());
        if (!CONFIRM_DUOC_TU.contains(current)) {
            throw new IllegalStateException("Chỉ có thể xác nhận khi trung tâm đã đề xuất lịch hẹn.");
        }
        if (bh.getThoiGianHen() == null || bh.getThoiGianHen().isBlank()) {
            throw new IllegalStateException("Chưa có lịch hẹn nào được đề xuất để xác nhận.");
        }

        bh.setTrangThai(TrangThai.DA_TIEP_NHAN.name());
        BaoHanh updated = repo.save(bh);
        log.info("Khách xác nhận lịch hẹn cho yêu cầu bảo hành ID: {}", id);
        return updated;
    }

    // ==========================================================
    // USER: yêu cầu đổi giờ hẹn (không tự chốt lịch mới)
    // ==========================================================
    @Transactional
    public BaoHanh requestReschedule(Integer id, String thoiGianMongMuon) {
        BaoHanh bh = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu bảo hành ID: " + id));

        TrangThai current = parseTrangThai(bh.getTrangThai());
        if (!RESCHEDULE_DUOC_TU.contains(current)) {
            throw new IllegalStateException("Chỉ có thể yêu cầu đổi giờ khi trung tâm đã đề xuất lịch hẹn.");
        }
        if (thoiGianMongMuon == null || thoiGianMongMuon.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn thời gian mong muốn.");
        }

        bh.setTrangThai(TrangThai.YEU_CAU_DOI_LICH.name());
        bh.setThoiGianKhachMongMuon(thoiGianMongMuon);

        BaoHanh updated = repo.save(bh);
        log.info("Khách hàng yêu cầu đổi lịch cho đơn bảo hành ID: {} với thời gian mong muốn: {}", id, thoiGianMongMuon);
        return updated;
    }

    private TrangThai parseTrangThai(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Trạng thái không được để trống.");
        }
        try {
            return TrangThai.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + raw);
        }
    }

    private void sendEmailToCustomerAsync(BaoHanh bh) {
        try {
            NguoiDung user = nguoiDungRepository.findById(bh.getMaNguoiDung()).orElse(null);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Không thể gửi email do không tìm thấy email của User ID: {}", bh.getMaNguoiDung());
                return;
            }

            String formattedTime = (bh.getThoiGianHen() != null && !bh.getThoiGianHen().isBlank())
                    ? bh.getThoiGianHen()
                    : "Sớm nhất có thể trong giờ hành chính";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("[VELORA CLOCK] Đề Xuất Lịch Hẹn Bảo Hành Đơn Hàng #" + bh.getMaDonHangCode());
            message.setText(
                    "Kính gửi Quý khách " + bh.getHoTen() + ",\n\n" +
                    "Yêu cầu bảo hành cho dòng sản phẩm [" + bh.getLoaiSanPham() + "] của Quý khách đã được hệ thống Velora tiếp nhận.\n\n" +
                    "LỊCH HẸN ĐỀ XUẤT:\n" +
                    "- Thời gian: " + formattedTime + "\n" +
                    "- Địa điểm: Showroom Velora Clock, 123 Lê Lợi, TP.HCM\n\n" +
                    "Vui lòng đăng nhập hệ thống để xác nhận hoặc yêu cầu đổi giờ hẹn.\n" +
                    "Mã yêu cầu: #" + bh.getMaBaoHanh() + "\n\n" +
                    "Trân trọng,\n" +
                    "Đội ngũ Velora Clock."
            );

            mailSender.send(message);
            log.info("Đã gửi email đề xuất lịch hẹn tới: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Gửi email thất bại: {}", e.getMessage(), e);
        }
    }
}