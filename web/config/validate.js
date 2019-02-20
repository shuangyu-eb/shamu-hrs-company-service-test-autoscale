module.exports = (k) => {
  if (!process.env.hasOwnProperty(k)) {
    throw new Error(`Missing environment variable: ${k}`);
  }
  return true;
};
