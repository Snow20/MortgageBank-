resource "azurerm_postgresql_flexible_server" "mortgage" {
  name                   = "mortgagebank-postgres"
  resource_group_name    = azurerm_resource_group.mortgage.name
  location               = azurerm_resource_group.mortgage.location
  version                = "15"
  administrator_login    = "psqladmin"
  administrator_password = "ComplexPassword123!"
  storage_mb             = 32768
  sku_name               = "GP_Standard_D2s_v3"
}