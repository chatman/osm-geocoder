package org.openstreetmap.osmgeocoder.indexer.primitives;

import com.sleepycat.persist.model.Persistent;
import java.io.Serializable;

@Persistent
public class Member implements Serializable {
  private static final long serialVersionUID = 3864617249278794035L;
  public Object member;
  public String role;

  public Member(Object member, String role) {
    this.member = member;
    this.role = role;
  }

  public String toString() {
    return this.member.getClass().getSimpleName() + ":" + this.member.toString();
  }
}
