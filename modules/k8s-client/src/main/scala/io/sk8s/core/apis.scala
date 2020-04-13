package io.sk8s.core

object apis {
  type Meta = io.sk8s.client.definitions.`io.k8s.apimachinery.pkg.apis.meta.v1.ObjectMeta`
  type Namespace = io.sk8s.client.definitions.`io.k8s.api.core.v1.Namespace`
  object Namespace {
    def apply(label: String): Namespace = new Namespace(Some("v1"), Some("Namespace"), Some(new Meta(name = Some(label))))
    val default: Namespace = Namespace("default")
  }
}
