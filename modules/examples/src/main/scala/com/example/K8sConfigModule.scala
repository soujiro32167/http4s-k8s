package com.example

import io.sk8s.core.configuration.Configuration
import zio.{Has, Layer, Task}

object K8sConfigModule {
  type K8sConfigModule = Has[Configuration]

  val live: Layer[Throwable, K8sConfigModule] = Task(Configuration.defaultK8sConfig).toLayer
}
