package model

import java.util.UUID
import org.joda.time.DateTime

case class AuthToken(
                      id: UUID,
                      userID: UUID,
                      expiry: DateTime)

