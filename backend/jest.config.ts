import type { Config } from 'jest';
import path from 'path';

const rootDir = path.resolve(__dirname);

const config: Config = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  rootDir,
  roots: [path.join(rootDir, 'tests')],
  testMatch: ['**/__tests__/**/*.ts', '**/?(*.)+(spec|test|tests).ts'],
  moduleNameMapper: {
    '^@/(.*)$': `${rootDir}/src/$1`,
  },
  setupFilesAfterEnv: [path.join(rootDir, 'jest.setup.ts')],
  collectCoverageFrom: [
    'src/**/*.ts',
    '!src/**/*.d.ts',
  ],
  transform: {
  '^.+\\.(ts|tsx)$': [
    'ts-jest',
    {
      tsconfig: 'tsconfig.test.json',
    },
  ],
},
};

export default config;