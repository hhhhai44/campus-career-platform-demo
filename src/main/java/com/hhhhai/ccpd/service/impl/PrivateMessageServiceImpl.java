package com.hhhhai.ccpd.service.impl;

import static com.hhhhai.ccpd.common.constant.RedisConstants.USER_PRIVATE_MESSAGE_UNREAD_COUNT_KEY;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhhhai.ccpd.common.context.UserContext;
import com.hhhhai.ccpd.common.context.UserContextHolder;
import com.hhhhai.ccpd.common.enums.ErrorCode;
import com.hhhhai.ccpd.common.enums.ReadStatusEnum;
import com.hhhhai.ccpd.dto.message.PrivateMessageSendDTO;
import com.hhhhai.ccpd.entity.message.PrivateMessageEntity;
import com.hhhhai.ccpd.entity.message.PrivateMessageSessionSettingEntity;
import com.hhhhai.ccpd.entity.user.UserEntity;
import com.hhhhai.ccpd.exception.BusinessException;
import com.hhhhai.ccpd.mapper.PrivateMessageMapper;
import com.hhhhai.ccpd.mapper.PrivateMessageSessionSettingMapper;
import com.hhhhai.ccpd.mapper.UserMapper;
import com.hhhhai.ccpd.mapper.projection.ConversationDigest;
import com.hhhhai.ccpd.service.PrivateMessageService;
import com.hhhhai.ccpd.vo.message.ConversationVO;
import com.hhhhai.ccpd.vo.message.PrivateMessageVO;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

  private static final String FALLBACK_USERNAME = "已注销用户";

  @Resource
  private PrivateMessageMapper privateMessageMapper;

  @Resource
  private UserMapper userMapper;

  @Resource
  private PrivateMessageSessionSettingMapper privateMessageSessionSettingMapper;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Page<ConversationVO> pageConversations(Long page, Long size) {
    Long userId = requireLoginUserId();

    long safePage = page == null || page < 1 ? 1 : page;
    long safeSize = size == null || size < 1 ? 10 : Math.min(size, 50);
    long offset = (safePage - 1) * safeSize;

    Long total = privateMessageMapper.countConversations(userId);
    if (total == null || total <= 0) {
      return new Page<>(safePage, safeSize);
    }

    List<ConversationDigest> digests =
        privateMessageMapper.selectConversationDigests(userId, offset, safeSize);
    if (digests.isEmpty()) {
      return new Page<>(safePage, safeSize, total);
    }

    Set<Long> messageIds = digests.stream().map(ConversationDigest::getLastMessageId)
        .filter(Objects::nonNull).collect(Collectors.toSet());
    Set<Long> peerUserIds = digests.stream().map(ConversationDigest::getPeerUserId)
        .filter(Objects::nonNull).collect(Collectors.toSet());

    Map<Long, PrivateMessageEntity> lastMessageMap = new HashMap<>();
    if (!messageIds.isEmpty()) {
      List<PrivateMessageEntity> messages = privateMessageMapper.selectBatchIds(messageIds);
      lastMessageMap = messages.stream()
          .collect(Collectors.toMap(PrivateMessageEntity::getId, m -> m));
    }

    Map<Long, String> userNameMap = loadUsernameMap(peerUserIds);
    Map<Long, PrivateMessageSessionSettingEntity> settingMap = loadSessionSettingMap(userId, peerUserIds);

    List<ConversationVO> records = new ArrayList<>();
    for (ConversationDigest digest : digests) {
      ConversationVO vo = new ConversationVO();
      vo.setPeerUserId(digest.getPeerUserId());
      vo.setPeerUsername(userNameMap.getOrDefault(digest.getPeerUserId(), FALLBACK_USERNAME));
      vo.setLastTime(digest.getLastTime());
      vo.setUnreadCount(digest.getUnreadCount() == null ? 0L : digest.getUnreadCount());

      PrivateMessageEntity last = lastMessageMap.get(digest.getLastMessageId());
      if (last != null) {
        vo.setLastSenderId(last.getFromUserId());
        vo.setLastMessage(buildDisplayContent(last, userId));
      }
      PrivateMessageSessionSettingEntity setting = settingMap.get(digest.getPeerUserId());
      vo.setPinned(setting != null && Objects.equals(setting.getIsPinned(), 1));
      vo.setMuted(setting != null && Objects.equals(setting.getIsMuted(), 1));
      records.add(vo);
    }

    Page<ConversationVO> result = new Page<>(safePage, safeSize, total);
    result.setRecords(records);
    return result;
  }

  @Override
  public Page<PrivateMessageVO> pageSessionMessages(Long peerUserId, Long page, Long size) {
    Long userId = requireLoginUserId();
    validatePeerUser(peerUserId, userId);

    long safePage = page == null || page < 1 ? 1 : page;
    long safeSize = size == null || size < 1 ? 20 : Math.min(size, 100);

    LambdaQueryWrapper<PrivateMessageEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.and(w -> w
            .and(a -> a.eq(PrivateMessageEntity::getFromUserId, userId)
                .eq(PrivateMessageEntity::getToUserId, peerUserId)
                .eq(PrivateMessageEntity::getFromDeleted, 0))
            .or(a -> a.eq(PrivateMessageEntity::getFromUserId, peerUserId)
                .eq(PrivateMessageEntity::getToUserId, userId)
                .eq(PrivateMessageEntity::getToDeleted, 0)))
        .orderByDesc(PrivateMessageEntity::getCreateTime)
        .orderByDesc(PrivateMessageEntity::getId);

    Page<PrivateMessageEntity> pageParam = new Page<>(safePage, safeSize);
    Page<PrivateMessageEntity> entityPage = privateMessageMapper.selectPage(pageParam, wrapper);
    List<PrivateMessageEntity> entities = entityPage.getRecords();

    Map<Long, String> userNameMap = loadUsernameMap(Set.of(userId, peerUserId));

    List<PrivateMessageVO> records = entities.stream().map(entity -> {
      PrivateMessageVO vo = new PrivateMessageVO();
      boolean mine = Objects.equals(entity.getFromUserId(), userId);
      boolean recalled = Objects.equals(entity.getIsRecalled(), 1);
      vo.setId(entity.getId());
      vo.setFromUserId(entity.getFromUserId());
      vo.setFromUsername(userNameMap.getOrDefault(entity.getFromUserId(), FALLBACK_USERNAME));
      vo.setToUserId(entity.getToUserId());
      vo.setToUsername(userNameMap.getOrDefault(entity.getToUserId(), FALLBACK_USERNAME));
      vo.setContent(buildDisplayContent(entity, userId));
      Integer isRead = entity.getIsRead() == null ? ReadStatusEnum.UNREAD.getCode()
          : entity.getIsRead().getCode();
      vo.setIsRead(isRead);
      vo.setCreateTime(entity.getCreateTime());
      vo.setMine(mine);
      vo.setRecalled(recalled);
      vo.setRecallable(isRecallable(entity, userId));
      return vo;
    }).collect(Collectors.toList());

    Page<PrivateMessageVO> result = new Page<>(safePage, safeSize, entityPage.getTotal());
    result.setRecords(records);
    return result;
  }

  @Override
  public void send(PrivateMessageSendDTO dto) {
    Long userId = requireLoginUserId();
    if (dto == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    Long toUserId = dto.getToUserId();
    validatePeerUser(toUserId, userId);

    String content = dto.getContent() == null ? "" : dto.getContent().trim();
    if (!StringUtils.hasText(content)) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    PrivateMessageEntity entity = new PrivateMessageEntity();
    entity.setFromUserId(userId);
    entity.setToUserId(toUserId);
    entity.setContent(content);
    entity.setIsRead(ReadStatusEnum.UNREAD);
    entity.setIsRecalled(0);
    entity.setFromDeleted(0);
    entity.setToDeleted(0);
    privateMessageMapper.insert(entity);

    increaseUnreadCount(toUserId);
  }

  @Override
  public void markSessionRead(Long peerUserId) {
    Long userId = requireLoginUserId();
    validatePeerUser(peerUserId, userId);

    LambdaQueryWrapper<PrivateMessageEntity> countWrapper = new LambdaQueryWrapper<>();
    countWrapper.eq(PrivateMessageEntity::getFromUserId, peerUserId)
        .eq(PrivateMessageEntity::getToUserId, userId)
        .eq(PrivateMessageEntity::getIsRead, ReadStatusEnum.UNREAD)
        .eq(PrivateMessageEntity::getToDeleted, 0);
    Long unread = privateMessageMapper.selectCount(countWrapper);
    if (unread == null || unread <= 0) {
      return;
    }

    PrivateMessageEntity update = new PrivateMessageEntity();
    update.setIsRead(ReadStatusEnum.READ);
    LambdaUpdateWrapper<PrivateMessageEntity> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.eq(PrivateMessageEntity::getFromUserId, peerUserId)
        .eq(PrivateMessageEntity::getToUserId, userId)
        .eq(PrivateMessageEntity::getIsRead, ReadStatusEnum.UNREAD)
        .eq(PrivateMessageEntity::getToDeleted, 0);
    privateMessageMapper.update(update, updateWrapper);

    decreaseUnreadCount(userId, unread);
  }

  @Override
  public Long getUnreadCount() {
    Long userId = requireLoginUserId();
    String key = USER_PRIVATE_MESSAGE_UNREAD_COUNT_KEY + userId;
    String cached = stringRedisTemplate.opsForValue().get(key);
    if (StringUtils.hasText(cached)) {
      try {
        return Long.parseLong(cached);
      } catch (NumberFormatException ignored) {
      }
    }

    Long count = privateMessageMapper.countUnreadByUserId(userId);
    if (count == null) {
      count = 0L;
    }
    stringRedisTemplate.opsForValue().set(key, String.valueOf(count));
    return count;
  }

  @Override
  public void setSessionPinned(Long peerUserId, Boolean pinned) {
    Long userId = requireLoginUserId();
    validatePeerUser(peerUserId, userId);
    upsertSessionSetting(userId, peerUserId, pinned, null);
  }

  @Override
  public void setSessionMuted(Long peerUserId, Boolean muted) {
    Long userId = requireLoginUserId();
    validatePeerUser(peerUserId, userId);
    upsertSessionSetting(userId, peerUserId, null, muted);
  }

  @Override
  public void recallMessage(Long messageId) {
    Long userId = requireLoginUserId();
    if (messageId == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }
    PrivateMessageEntity entity = privateMessageMapper.selectById(messageId);
    if (entity == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }
    if (!Objects.equals(entity.getFromUserId(), userId)) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    if (Objects.equals(entity.getIsRecalled(), 1)) {
      return;
    }
    if (entity.getCreateTime() == null
        || LocalDateTime.now().isAfter(entity.getCreateTime().plusMinutes(2))) {
      throw new BusinessException(ErrorCode.MESSAGE_RECALL_TIMEOUT);
    }
    PrivateMessageEntity update = new PrivateMessageEntity();
    update.setIsRecalled(1);
    LambdaUpdateWrapper<PrivateMessageEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PrivateMessageEntity::getId, messageId)
        .eq(PrivateMessageEntity::getFromUserId, userId)
        .eq(PrivateMessageEntity::getIsRecalled, 0);
    privateMessageMapper.update(update, wrapper);
  }

  @Override
  public void deleteMessageForMe(Long messageId) {
    Long userId = requireLoginUserId();
    if (messageId == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }
    PrivateMessageEntity entity = privateMessageMapper.selectById(messageId);
    if (entity == null) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }

    PrivateMessageEntity update = new PrivateMessageEntity();
    LambdaUpdateWrapper<PrivateMessageEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(PrivateMessageEntity::getId, messageId);
    if (Objects.equals(entity.getFromUserId(), userId)) {
      update.setFromDeleted(1);
    } else if (Objects.equals(entity.getToUserId(), userId)) {
      update.setToDeleted(1);
      if (!Objects.equals(entity.getToDeleted(), 1) && entity.getIsRead() == ReadStatusEnum.UNREAD) {
        decreaseUnreadCount(userId, 1L);
      }
    } else {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    privateMessageMapper.update(update, wrapper);
  }

  private Long requireLoginUserId() {
    UserContext user = UserContextHolder.getUser();
    if (user == null || user.getUserId() == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
    return user.getUserId();
  }

  private void validatePeerUser(Long peerUserId, Long selfUserId) {
    if (peerUserId == null || Objects.equals(peerUserId, selfUserId)) {
      throw new BusinessException(ErrorCode.PARAM_INVALID);
    }
    if (userMapper.selectById(peerUserId) == null) {
      throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
  }

  private Map<Long, String> loadUsernameMap(Set<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Map.of();
    }
    List<UserEntity> users = userMapper.selectBatchIds(userIds);
    return users.stream().collect(Collectors.toMap(UserEntity::getId,
        u -> StringUtils.hasText(u.getUsername()) ? u.getUsername()
            : (StringUtils.hasText(u.getRealName()) ? u.getRealName() : FALLBACK_USERNAME)));
  }

  private Map<Long, PrivateMessageSessionSettingEntity> loadSessionSettingMap(Long userId,
      Set<Long> peerUserIds) {
    if (peerUserIds == null || peerUserIds.isEmpty()) {
      return Map.of();
    }
    List<PrivateMessageSessionSettingEntity> settings =
        privateMessageSessionSettingMapper.selectList(
            new LambdaQueryWrapper<PrivateMessageSessionSettingEntity>()
                .eq(PrivateMessageSessionSettingEntity::getUserId, userId)
                .in(PrivateMessageSessionSettingEntity::getPeerUserId, peerUserIds));
    return settings.stream().collect(Collectors.toMap(
        PrivateMessageSessionSettingEntity::getPeerUserId, s -> s, (a, b) -> a));
  }

  private void upsertSessionSetting(Long userId, Long peerUserId, Boolean pinned, Boolean muted) {
    PrivateMessageSessionSettingEntity setting = privateMessageSessionSettingMapper.selectOne(
        new LambdaQueryWrapper<PrivateMessageSessionSettingEntity>()
            .eq(PrivateMessageSessionSettingEntity::getUserId, userId)
            .eq(PrivateMessageSessionSettingEntity::getPeerUserId, peerUserId));

    if (setting == null) {
      setting = new PrivateMessageSessionSettingEntity();
      setting.setUserId(userId);
      setting.setPeerUserId(peerUserId);
      setting.setIsPinned(Boolean.TRUE.equals(pinned) ? 1 : 0);
      setting.setIsMuted(Boolean.TRUE.equals(muted) ? 1 : 0);
      privateMessageSessionSettingMapper.insert(setting);
      return;
    }

    if (pinned != null) {
      setting.setIsPinned(Boolean.TRUE.equals(pinned) ? 1 : 0);
    }
    if (muted != null) {
      setting.setIsMuted(Boolean.TRUE.equals(muted) ? 1 : 0);
    }
    privateMessageSessionSettingMapper.updateById(setting);
  }

  private void increaseUnreadCount(Long userId) {
    String key = USER_PRIVATE_MESSAGE_UNREAD_COUNT_KEY + userId;
    stringRedisTemplate.opsForValue().increment(key);
  }

  private void decreaseUnreadCount(Long userId, Long delta) {
    if (delta == null || delta <= 0) {
      return;
    }
    String key = USER_PRIVATE_MESSAGE_UNREAD_COUNT_KEY + userId;
    Long val = stringRedisTemplate.opsForValue().decrement(key, delta);
    if (val != null && val < 0) {
      stringRedisTemplate.opsForValue().set(key, "0");
    }
  }

  private String buildDisplayContent(PrivateMessageEntity message, Long currentUserId) {
    if (!Objects.equals(message.getIsRecalled(), 1)) {
      return message.getContent();
    }
    return Objects.equals(message.getFromUserId(), currentUserId)
        ? "你撤回了一条消息"
        : "对方撤回了一条消息";
  }

  private boolean isRecallable(PrivateMessageEntity message, Long currentUserId) {
    if (!Objects.equals(message.getFromUserId(), currentUserId)) {
      return false;
    }
    if (Objects.equals(message.getIsRecalled(), 1) || message.getCreateTime() == null) {
      return false;
    }
    return !LocalDateTime.now().isAfter(message.getCreateTime().plusMinutes(2));
  }
}




