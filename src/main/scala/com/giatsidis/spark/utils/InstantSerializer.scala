package com.giatsidis.spark.utils

import org.json4s.CustomSerializer
import java.time.Instant
import org.json4s.JsonAST.{JNull, JString}

case object InstantSerializer extends CustomSerializer[Instant](_ => ( {
  case JString(string) => Instant.parse(string)
  case JNull => null
}, {
  case instant: Instant => {
    JString(instant.toString())
  }
}
))
