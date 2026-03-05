package com.hhhhai.ccpd.common.constant;

public class RedisConstants {

  public static final String LOGIN_CODE_KEY = "login:code:";
  public static final Long LOGIN_CODE_TTL = 2L;
  public static final String LOGIN_USER_KEY = "login:token:";
  public static final Long LOGIN_USER_TTL = 36000L;

  public static final Long CACHE_NULL_TTL = 2L;

  public static final Long CACHE_SHOP_TTL = 30L;
  public static final String CACHE_SHOP_KEY = "cache:shop:";
  public static final String CACHE_SHOP_TYPE_KEY = "cache:shoptype:";

  public static final String LOCK_SHOP_KEY = "lock:shop:";
  public static final Long LOCK_SHOP_TTL = 10L;

  public static final String SECKILL_STOCK_KEY = "seckill:stock:";
  public static final String BLOG_LIKED_KEY = "blog:liked:";
  public static final String FEED_KEY = "feed:";
  public static final String SHOP_GEO_KEY = "shop:geo:";
  public static final String USER_SIGN_KEY = "sign:";

  /**
   * 帖子点赞缓存 key 前缀，完整 key = post:like:{postId}
   */
  public static final String POST_LIKE_KEY = "post:like:";

  /**
   * 用户点赞帖子记录 key 前缀，完整 key = user:like:post:{userId}
   */
  public static final String USER_LIKE_POST_KEY = "user:like:post:";

  /**
   * 热门帖子榜单 key
   */
  public static final String HOT_POST_LIST_KEY = "hot:post:list";
}
