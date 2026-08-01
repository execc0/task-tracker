package org.example.task_tracker.service;

import lombok.extern.slf4j.Slf4j;
import org.example.task_tracker.exception.SocialLinkException;
import org.example.task_tracker.model.Social;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.SocialRepository;
import org.example.task_tracker.security.DTO.social.UnlinkSocialRequest;
import org.example.task_tracker.security.DTO.social.signable.LinkRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
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
    public void linkSocial(LinkRequest request) {
        User user = userService.getCurrentUser();
        Optional<Social> social = socialRepository.findByProviderAndProviderId(request.getProvider().toLowerCase(Locale.ROOT), request.getProviderId());
        if (social.isPresent()) {
            if (social.get().getUser().getId() != user.getId()) {
                throw new SocialLinkException("Данная связь уже существует у другого пользователя");
            }
            return;
        }
        Social newSocial = new Social(user, request.getProvider().toLowerCase(Locale.ROOT), request.getProviderId());
        socialRepository.save(newSocial);
    }

    @Transactional
    public void unlinkSocial(UnlinkSocialRequest request) {
        User user = userService.getCurrentUser();
        Social social = socialRepository.findByProviderAndProviderId(request.getProvider().toLowerCase(Locale.ROOT), request.getProviderId()).
                orElseThrow(() -> new SocialLinkException("Данной связи с вашим аккаунтом не существует"));
        if (social.getUser().getId() != user.getId()) {
            log.debug("CurrentUser: {}, UserRequest: {}", user.getId(), social.getUser().getId());
            throw new SocialLinkException("Данной связи с вашим аккаунтом не существует");
        }
        socialRepository.delete(social);

    }
}
