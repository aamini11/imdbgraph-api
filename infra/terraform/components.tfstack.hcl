component "server" {
  source = "./server"

  providers = {
    aws    = provider.aws.configurations[each.value]
    random = provider.random.this
  }
}