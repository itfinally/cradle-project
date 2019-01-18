package io.github.itfinally.vo;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings( "unchecked" )
public class BasicVo<Vo extends BasicVo<Vo>> implements Serializable {
  private long id;
  private boolean deleted;
  private long createTime;
  private long updateTime;

  public long getId() {
    return id;
  }

  public Vo setId( long id ) {
    this.id = id;
    return ( Vo ) this;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public Vo setDeleted( boolean deleted ) {
    this.deleted = deleted;
    return ( Vo ) this;
  }

  public long getCreateTime() {
    return createTime;
  }

  public Vo setCreateTime( long createTime ) {
    this.createTime = createTime;
    return ( Vo ) this;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public Vo setUpdateTime( long updateTime ) {
    this.updateTime = updateTime;
    return ( Vo ) this;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) return true;
    if ( o == null || getClass() != o.getClass() ) return false;
    BasicVo<?> basicVo = ( BasicVo<?> ) o;
    return id == basicVo.id &&
        deleted == basicVo.deleted &&
        createTime == basicVo.createTime &&
        updateTime == basicVo.updateTime;
  }

  @Override
  public int hashCode() {
    return Objects.hash( id, deleted, createTime, updateTime );
  }

  @Override
  public String toString() {
    return "BasicVo{" +
        "id=" + id +
        ", deleted=" + deleted +
        ", createTime=" + createTime +
        ", updateTime=" + updateTime +
        '}';
  }
}
