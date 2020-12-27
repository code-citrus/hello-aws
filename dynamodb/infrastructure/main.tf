resource "aws_dynamodb_table" "music" {
    name = "hello-ddb-music"
    billing_mode   = PROVISIONED
    read_capacity  = 4
    write_capacity = 4
    hash_key       = "artist"
    range_key      = "song" 
}