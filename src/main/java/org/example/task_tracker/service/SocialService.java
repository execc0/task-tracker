package org.example.task_tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.example.task_tracker.exception.SocialLinkException;
import org.example.task_tracker.model.Social;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.SocialRepository;
import org.example.task_tracker.security.DTO.LinkSocialRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class SocialService {

    private final SocialRepository socialRepository;
    private final UserService userService;

    public SocialService(SocialRepository socialRepository, UserService userService) {
        this.socialRepository = socialRepository;
        this.userService = userService;
    }

    @Transactional
    public void linkSocial(LinkSocialRequest request) {
        User user = userService.getCurrentUser();
        Optional<Social> social = socialRepository.findByProviderAndProviderId(request.getProvider(), request.getProviderId());
        if (social.isPresent()) {
            if (!social.get().getUser().equals(user)) {
                throw new SocialLinkException("Данная связь уже существует у другого пользователя");
            }
        }
        Social newSocial = new Social(user, request.getProvider(), request.getProviderId());
        socialRepository.save(newSocial);
    }
}
