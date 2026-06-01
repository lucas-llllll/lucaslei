import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.lucaslei.app',
  appName: 'Lucaslei',
  webDir: 'dist',
  server: {
    androidScheme: 'https'
  }
};

export default config;
