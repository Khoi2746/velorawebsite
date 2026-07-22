package com.velora.website.Service;

import com.velora.website.Entity.BaoHanh;
import com.velora.website.Repository.BaoHanhRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaoHanhService {

    private final BaoHanhRepository repo;

    BaoHanhService(BaoHanhRepository repo) {
        this.repo = repo;
    }

    // ===========================
    // Lưu yêu cầu bảo hành
    // ===========================
    public BaoHanh saveRequest(BaoHanh baoHanh) {

        baoHanh.setTrangThai("CHO_XU_LY");

        return repo.save(baoHanh);

    }

    // ===========================
    // Admin xem tất cả
    // ===========================
    public List<BaoHanh> getAllRequests() {

        return repo.findAll();

    }

    // ===========================
    // Admin xem yêu cầu chờ xử lý
    // ===========================
    public List<BaoHanh> getPendingRequests() {

        return repo.findByTrangThai("CHO_XU_LY");

    }

    // ===========================
    // User xem lịch sử của mình
    // ===========================
    public List<BaoHanh> findByMaNguoiDung(Integer maNguoiDung) {

        return repo.findByMaNguoiDung(maNguoiDung);

    }

    // ===========================
    // Admin cập nhật trạng thái
    // ===========================
    public BaoHanh updateStatus(
            Integer id,
            String trangThai) {

        BaoHanh bh = repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy yêu cầu bảo hành"));

        switch (trangThai) {

            case "CHO_XU_LY":
            case "DA_TIEP_NHAN":
            case "DANG_XU_LY":
            case "HOAN_TAT":
            case "TU_CHOI":
                bh.setTrangThai(trangThai.trim().toUpperCase());
                break;

            default:
                throw new RuntimeException("Trạng thái không hợp lệ");

        }

        return repo.save(bh);

    }
public BaoHanh cancelRequest(Integer id){

    BaoHanh bh = repo.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Không tìm thấy yêu cầu"));

    // chỉ được hủy khi chờ xử lý
    if(!bh.getTrangThai().equals("CHO_XU_LY")){
        throw new RuntimeException(
                "Không thể hủy yêu cầu này."
        );
    }


    bh.setTrangThai("DA_HUY");


    return repo.save(bh);

}
}