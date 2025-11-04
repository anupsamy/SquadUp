import { sanitizeArgs, sanitizeInput } from '../utils/sanitizeInput.util';

const logger = {
  info: (message: string, ...args: unknown[]) => {
    const logMessage = `[INFO] ${sanitizeInput(message)}`;
    const sanitizedArgs = sanitizeArgs(args);
    // eslint-disable-next-line no-console
    console.log(logMessage, ...sanitizedArgs);
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
