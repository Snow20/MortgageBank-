resource "azurerm_container_registry" "mortgage" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.mortgage.name
  location            = azurerm_resource_group.mortgage.location
  sku                 = "Standard"
  admin_enabled       = true
}