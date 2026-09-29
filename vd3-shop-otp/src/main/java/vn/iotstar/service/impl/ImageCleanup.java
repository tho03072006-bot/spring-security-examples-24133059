package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vn.iotstar.service.CloudinaryService;

// File ảnh không tham gia transaction của database: xử lý theo kết quả commit/rollback.
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageCleanup {

    private final CloudinaryService imageService;

    public void afterCommit(String publicId) {
        schedule(publicId, TransactionSynchronization.STATUS_COMMITTED);
    }

    public void afterRollback(String publicId) {
        schedule(publicId, TransactionSynchronization.STATUS_ROLLED_BACK);
    }

    private void schedule(String publicId, int expectedStatus) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == expectedStatus) {
                    try {
                        imageService.delete(publicId);
                    } catch (RuntimeException exception) {
                        // Dữ liệu đã commit/rollback; giữ lại ID để có thể dọn ảnh khi dịch vụ phục hồi.
                        log.warn("Không thể dọn ảnh {} sau giao dịch; cần thử xóa lại", publicId, exception);
                    }
                }
            }
        });
    }
}
