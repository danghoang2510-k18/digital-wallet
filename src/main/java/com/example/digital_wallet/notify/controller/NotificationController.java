package com.example.digital_wallet.notify.controller;




import com.example.digital_wallet.common.response.ApiResponse;
import com.example.digital_wallet.common.security.CurrentUserService;
import com.example.digital_wallet.notify.response.NotificationResponse;
import com.example.digital_wallet.notify.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class NotificationController {

    NotificationService notificationService;
    CurrentUserService currentUserService;

    @GetMapping("/get-notify")
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {


        return ApiResponse.<Page<NotificationResponse>>builder()
                .result(notificationService.getNotifications(page,size))
                .build();
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {



        return ApiResponse.<Long>builder()
                .result(notificationService.getUnreadCount())
                .build();
    }



    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable UUID notificationId
    ) {


        notificationService.markAsRead(
                notificationId
        );

        return ApiResponse.<Void>builder().build();
    }


}