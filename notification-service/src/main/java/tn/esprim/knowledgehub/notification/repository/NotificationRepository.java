package tn.esprim.knowledgehub.notification.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.notification.entity.Notification;
import tn.esprim.knowledgehub.notification.entity.NotificationType;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserId(String userId);

    List<Notification> findByUserIdAndType(String userId, NotificationType type);

    List<Notification> findByUserIdAndDelivered(String userId, boolean delivered);

    List<Notification> findByUserIdAndRead(String userId, boolean read);
}
