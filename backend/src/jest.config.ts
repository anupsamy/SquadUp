import { pathsToModuleNameMapper } from 'ts-jest';
import tsconfig from '../tsconfig.json';
const { compilerOptions } = tsconfig;

export default {
  preset: 'ts-jest/presets/default-esm',
  testEnvironment: 'node',
  moduleNameMapper: pathsToModuleNameMapper(compilerOptions.paths || {}, {
    prefix: '<rootDir>/src/',
  }),
  moduleDirectories: ['node_modules', 'src'],
  roots: ['<rootDir>/src/tests'],
  extensionsToTreatAsEsm: ['.ts'],
  transform: {
    '^.+\\.ts$': ['ts-jest', { useESM: true }],
  },
};
