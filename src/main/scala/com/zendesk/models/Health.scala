package com.zendesk.models


final case class PingResponse(status: String)
final case class DiagnosticResult(success: Boolean, message: String)
