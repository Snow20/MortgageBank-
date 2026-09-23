output "resource_group" {
  value = azurerm_resource_group.mortgage.name
}

output "aks_name" {
  value = azurerm_kubernetes_cluster.mortgage.name
}

output "acr_login_server" {
  value = azurerm_container_registry.mortgage.login_server
}

output "postgres_server" {
  value = azurerm_postgresql_flexible_server.mortgage.fqdn
}