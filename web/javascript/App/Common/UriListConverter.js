define(() => ({
  read(str /* , opts */) {
    return str.split('\n');
  },
  write(obj /* , opts */) {
    // If this is an Array, extract the self URI and then join using a newline
    if (obj instanceof Array) {
      return obj.map(resource => resource._links.self.href).join('\n');
    }
    // otherwise, just return the self URI
    return obj._links.self.href;
  },
}));

