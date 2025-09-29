variable "region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "리소스 이름 prefix"
  type        = string
  default     = "devcos-team10"
}