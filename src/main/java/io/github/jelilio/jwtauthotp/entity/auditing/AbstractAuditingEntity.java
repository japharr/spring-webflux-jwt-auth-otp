package io.github.jelilio.jwtauthotp.entity.auditing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;
import java.time.Instant;

@Data
public class AbstractAuditingEntity implements Serializable {
  @JsonIgnore
  @CreatedBy
  @Column("created_by")
  private String createdBy;

  @JsonIgnore
  @CreatedDate
  @Column("created_date")
  private Instant createdDate;

  @JsonIgnore
  @LastModifiedBy
  @Column("last_modified_by")
  private String lastModifiedBy;

  @JsonIgnore
  @LastModifiedDate
  @Column("last_modified_date")
  private Instant lastModifiedDate;

}
