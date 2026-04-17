package com.hhhhai.ccpd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhhhai.ccpd.entity.message.PrivateMessageEntity;
import com.hhhhai.ccpd.mapper.projection.ConversationDigest;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessageEntity> {

  @Select("""
      SELECT COUNT(DISTINCT CASE
        WHEN from_user_id = #{userId} THEN to_user_id ELSE from_user_id END)
      FROM private_message
      WHERE ((from_user_id = #{userId} AND IFNULL(from_deleted, 0) = 0)
         OR (to_user_id = #{userId} AND IFNULL(to_deleted, 0) = 0))
      """)
  Long countConversations(@Param("userId") Long userId);

  @Select("""
      SELECT
        peer_user_id AS peerUserId,
        MAX(id) AS lastMessageId,
        MAX(create_time) AS lastTime,
        SUM(CASE WHEN to_user_id = #{userId} AND is_read = 0 THEN 1 ELSE 0 END) AS unreadCount
      FROM (
        SELECT
          id,
          create_time,
          to_user_id,
          is_read,
          CASE WHEN from_user_id = #{userId} THEN to_user_id ELSE from_user_id END AS peer_user_id
        FROM private_message
        WHERE ((from_user_id = #{userId} AND IFNULL(from_deleted, 0) = 0)
           OR (to_user_id = #{userId} AND IFNULL(to_deleted, 0) = 0))
      ) t
      GROUP BY peer_user_id
      ORDER BY lastTime DESC
      LIMIT #{offset}, #{size}
      """)
  List<ConversationDigest> selectConversationDigests(@Param("userId") Long userId,
      @Param("offset") long offset, @Param("size") long size);

  @Select("""
      SELECT IFNULL(COUNT(1), 0)
      FROM private_message
      WHERE to_user_id = #{userId} AND is_read = 0 AND IFNULL(to_deleted, 0) = 0
      """)
  Long countUnreadByUserId(@Param("userId") Long userId);
}


