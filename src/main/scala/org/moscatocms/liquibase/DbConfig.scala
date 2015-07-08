package org.moscatocms.liquibase

case class DbConfig(
  url: String,
  driver: String,
  username: String,
  password: String
)