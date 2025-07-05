package com.example.skill_management.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "token")
public class Token {

  @Id
  
  private Integer id;

  @Column("token")
  private String token;

  @Column("token_type")
  private TokenType tokenType = TokenType.BEARER;

  @Column("revoked")
  private boolean revoked;

  @Column("expired")
  private boolean expired;

  @Column("user_id")
  private Integer userId;
}
