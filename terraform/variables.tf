variable "location" {
  type        = string
  description = "Azure region"
  default     = "eastus"
}

variable "resource_group_name" {
  type        = string
  description = "Resource Group Name"
  default     = "rg-mortgage-bank"
}

variable "aks_name" {
  type        = string
  description = "AKS Cluster Name"
  default     = "aks-mortgage-cluster"
}

variable "acr_name" {
  type        = string
  description = "Azure Container Registry Name"
  default     = "acrmortgagebank123"
}