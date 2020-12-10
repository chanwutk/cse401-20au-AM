
try {
  a + b;
} catch (RuntimeException exc) {
  a - b;
}

try {
  a + b;
} catch (Test exc) {
  a - b;
}