package org.hhhhai.campuscareerplatformdemo.common.context;


public class UserContextHolder {

  private static final ThreadLocal<UserContext> tl = new ThreadLocal<>();

  public static void saveUser(UserContext user) {
    tl.set(user);
  }

  public static UserContext getUser() {
    return tl.get();
  }

  public static void removeUser() {
    tl.remove();
  }
}
