package com.parkeye.pvs.service;

import com.parkeye.pvs.account.entity.UserEntity;
import com.parkeye.pvs.account.repository.SavedCardRepository;
import com.parkeye.pvs.account.repository.UserRepository;
import com.parkeye.pvs.dto.ConsumerDtos.ProfileDto;
import com.parkeye.pvs.dto.ConsumerDtos.SavedCardDto;
import com.parkeye.pvs.dto.ConsumerDtos.UpdateSettingsRequest;
import com.parkeye.pvs.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfileService {

    private final UserRepository users;
    private final SavedCardRepository cards;

    public ProfileService(UserRepository users, SavedCardRepository cards) {
        this.users = users;
        this.cards = cards;
    }

    @Transactional(readOnly = true)
    public ProfileDto get(long userId) {
        return toDto(load(userId));
    }

    @Transactional
    public ProfileDto updateSettings(long userId, UpdateSettingsRequest req) {
        UserEntity u = load(userId);
        if (req.smsNotifications() != null) {
            // NOTE: only stored for now - nothing sends SMS yet (see README / change notes).
            u.setSmsNotifications(req.smsNotifications());
        }
        return toDto(users.save(u));
    }

    @Transactional(readOnly = true)
    public List<SavedCardDto> savedCards(long userId) {
        return cards.findByUserIdOrderByIdAsc(userId).stream()
                .map(c -> new SavedCardDto(c.getId(), c.getBrand(), c.getLast4(),
                        c.getExpiryMonth() != null ? c.getExpiryMonth() : 0,
                        c.getExpiryYear() != null ? c.getExpiryYear() : 0,
                        Boolean.TRUE.equals(c.getPrimaryCard())))
                .toList();
    }

    private UserEntity load(long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found."));
    }

    private ProfileDto toDto(UserEntity u) {
        return new ProfileDto(u.getId(), u.getFullName(), initials(u.getFullName()), u.getEmail(),
                Boolean.TRUE.equals(u.getVerified()), u.getMunicipality(), u.getLanguage(),
                Boolean.TRUE.equals(u.getSmsNotifications()));
    }

    private static String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        StringBuilder sb = new StringBuilder();
        for (String part : fullName.trim().split("\\s+")) {
            if (sb.length() >= 2) break;
            sb.append(part.substring(0, 1).toUpperCase());
        }
        return sb.toString();
    }
}
