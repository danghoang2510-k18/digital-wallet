package com.example.digital_wallet.notify.mapper;


import com.example.digital_wallet.notify.entity.Notification;
import com.example.digital_wallet.notify.response.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotifyMapper {

    public NotificationResponse toNotificationResponse(Notification notification);
}
