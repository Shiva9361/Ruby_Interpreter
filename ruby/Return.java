package ruby;

/*
 * wraps the return value with the accoutrements Java requires for a runtime exception class
 */

class Return extends RuntimeException {
    final Object value;
    Return(Object value) {
      super(null, null, false, false);
      this.value = value;
    } 
}