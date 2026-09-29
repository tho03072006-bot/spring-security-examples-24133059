package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vn.iotstar.security.CustomUserDetails;

@Service
@RequiredArgsConstructor
public class AccountSessionService {

    private final SessionRegistry sessionRegistry;

    // Chỉ thu hồi phiên khi thay đổi tài khoản đã được lưu thành công.
    // ConcurrentSessionFilter sẽ chặn phiên cũ ngay ở request kế tiếp.
    public void expireAfterCommit(Long userId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Object principal : sessionRegistry.getAllPrincipals()) {
                    if (principal instanceof CustomUserDetails user && userId.equals(user.getId())) {
                        sessionRegistry.getAllSessions(principal, false)
                                .forEach(session -> session.expireNow());
                    }
                }
            }
        });
    }
}
