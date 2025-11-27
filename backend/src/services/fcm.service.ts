import admin from 'firebase-admin';
import logger from '../utils/logger.util';

let initialized = false;

export function initialize() {
  if (initialized) return;
  try {
    const keyJson = process.env.FIREBASE_SERVICE_ACCOUNT_KEY;
    if (!keyJson) {
      logger.warn('FIREBASE_SERVICE_ACCOUNT_KEY is not set; FCM disabled');
      return;
    }
    interface ServiceAccount {
      type?: string;
      project_id?: string;
      private_key_id?: string;
      private_key?: string;
      client_email?: string;
      client_id?: string;
      auth_uri?: string;
      token_uri?: string;
      auth_provider_x509_cert_url?: string;
      client_x509_cert_url?: string;
    }
    const serviceAccount = JSON.parse(keyJson as string) as ServiceAccount;
    // Normalize private_key newlines if the JSON contains escaped \n sequences
    if (typeof serviceAccount.private_key === 'string') {
      serviceAccount.private_key = serviceAccount.private_key.replace(
        /\\n/g,
        '\n'
      );
    }
    // admin.credential.cert accepts string | ServiceAccount
    // Cast to unknown first, then to the expected type to satisfy TypeScript
    admin.initializeApp({
      credential: admin.credential.cert(
        serviceAccount as unknown as admin.ServiceAccount
      ),
    });
    initialized = true;
    logger.info('Firebase Admin initialized');
  } catch (err) {
    logger.error('Failed to initialize Firebase Admin', err as Error);
  }
}

export type FcmPayload = {
  title: string;
  body: string;
  data?: Record<string, string>;
};

export async function sendToTokens(tokens: string[], payload: FcmPayload) {
  initialize();
  if (!initialized || tokens.length === 0)
    return { success: 0, failure: tokens.length };
  try {
    const res = await admin.messaging().sendEachForMulticast({
      tokens,
      notification: { title: payload.title, body: payload.body },
      data: payload.data,
    });
    logger.info(`FCM: success=${res.successCount} failure=${res.failureCount}`);
    return { success: res.successCount, failure: res.failureCount };
  } catch (e) {
    logger.error('FCM send error', e as Error);
    return { success: 0, failure: tokens.length };
  }
}

export async function sendToTopic(topic: string, payload: FcmPayload) {
  initialize();
  if (!initialized) return { success: 0, failure: 1 };
  try {
    const res = await admin.messaging().send({
      topic,
      notification: { title: payload.title, body: payload.body },
      data: payload.data,
    });
    logger.info(`FCM topic send id=${res} topic=${topic}`);
    return { success: 1, failure: 0 };
  } catch (e) {
    logger.error('FCM topic send error', e as Error);
    return { success: 0, failure: 1 };
  }
}

export async function sendGroupJoinFCM(
  joinCode: string,
  userName: string,
  groupName: string,
  actingUserId: string
) {
  const title = 'Group Member Joined';
  const body = `${userName} joined the group "${groupName}"`;
  return sendToTopic(joinCode, {
    title,
    body,
    data: {
      type: 'group_join',
      joinCode,
      userName,
      groupName,
      timestamp: new Date().toISOString(),
      actingUserId, // new field for filtering on frontend
    },
  });
}

export async function sendGroupLeaveFCM(
  joinCode: string,
  userName: string,
  groupName: string,
  actingUserId: string
) {
  const title = 'Group Member Left';
  const body = `${userName} left the group "${groupName}"`;
  return sendToTopic(joinCode, {
    title,
    body,
    data: {
      type: 'group_leave',
      joinCode,
      userName,
      groupName,
      timestamp: new Date().toISOString(),
      actingUserId, // new field for filtering on frontend
    },
  });
}

export async function sendActivitySelectedFCM(
  joinCode: string,
  activityName: string,
  groupName: string,
  leaderId: string,
  activityData?: string
) {
  const title = 'Activity Selected';
  const body = `Group leader selected "${activityName}" for the group "${groupName}"`;
  return sendToTopic(joinCode, {
    title,
    body,
    data: {
      type: 'activity_selected',
      joinCode,
      activityName,
      groupName,
      timestamp: new Date().toISOString(),
      actingUserId: leaderId, // new field for filtering on frontend
      activityData: activityData || '', // Optional activity data as JSON string
    },
  });
}
