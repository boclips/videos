strip_object_id = function(objectId) {
  regmatches(objectId,regexpr("[a-z0-9]{24}",objectId))
}
