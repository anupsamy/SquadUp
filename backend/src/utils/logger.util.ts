import { sanitizeArgs, sanitizeInput } from '../utils/sanitizeInput.util';

const logger = {
  info: (message: string, ...args: unknown[]) => {
    const logMessage = `[INFO] ${sanitizeInput(message)}`;
    const sanitizedArgs = sanitizeArgs(args);
    // eslint-disable-next-line no-console
    // Construct message string first to satisfy linter
    const finalMessage = logMessage;
    // Use apply to avoid spread operator with non-literal args
    if (sanitizedArgs.length > 0) {
      console.log(finalMessage, ...sanitizedArgs);
    } else {
      console.log(finalMessage);
    }
  },
  error: (message: string, ...args: unknown[]) => {
    const logMessage = `[ERROR] ${sanitizeInput(message)}`;
    const sanitizedArgs = sanitizeArgs(args);
    // eslint-disable-next-line no-console
    console.error(logMessage, ...sanitizedArgs);
  },
  warn: (message: string, ...args: unknown[]) => {
    const logMessage = `[WARN] ${sanitizeInput(message)}`;
    const sanitizedArgs = sanitizeArgs(args);
    // eslint-disable-next-line no-console
    console.warn(logMessage, ...sanitizedArgs);
  },
  debug: (message: string, ...args: unknown[]) => {
    const logMessage = `[DEBUG] ${sanitizeInput(message)}`;
    const sanitizedArgs = sanitizeArgs(args);
    // eslint-disable-next-line no-console
    console.debug(logMessage, ...sanitizedArgs);
  },
};

export default logger;
