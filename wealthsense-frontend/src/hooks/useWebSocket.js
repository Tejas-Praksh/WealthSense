import { useEffect, useRef, useCallback, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { addTransaction } from '@/store/slices/transactionSlice';
import { addNotification } from '@/store/slices/notificationSlice';
import { WS_URL } from '@/utils/constants';

export function useWebSocket() {
  const dispatch = useDispatch();
  const { accessToken, isAuthenticated, user } = useSelector((state) => state.auth);
  const clientRef = useRef(null);
  const [connected, setConnected] = useState(false);

  const connect = useCallback(() => {
    if (!isAuthenticated || !accessToken) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setConnected(true);
        console.log('[WS] Connected');

        // Subscribe to user-specific channel
        if (user?.id) {
          client.subscribe(`/user/${user.id}/notifications`, (message) => {
            try {
              const data = JSON.parse(message.body);
              dispatch(addNotification(data));
            } catch (e) {
              console.error('[WS] Parse error:', e);
            }
          });

          client.subscribe(`/user/${user.id}/transactions`, (message) => {
            try {
              const data = JSON.parse(message.body);
              dispatch(addTransaction(data));
            } catch (e) {
              console.error('[WS] Parse error:', e);
            }
          });

          // Fraud alerts — high priority
          client.subscribe(`/user/${user.id}/fraud-alerts`, (message) => {
            try {
              const data = JSON.parse(message.body);
              dispatch(
                addNotification({
                  ...data,
                  type: 'FRAUD_ALERT',
                  urgent: true,
                })
              );
            } catch (e) {
              console.error('[WS] Parse error:', e);
            }
          });
        }
      },
      onDisconnect: () => {
        setConnected(false);
        console.log('[WS] Disconnected');
      },
      onStompError: (frame) => {
        console.error('[WS] STOMP error:', frame.headers['message']);
      },
    });

    client.activate();
    clientRef.current = client;
  }, [isAuthenticated, accessToken, user, dispatch]);

  const disconnect = useCallback(() => {
    if (clientRef.current) {
      clientRef.current.deactivate();
      clientRef.current = null;
      setConnected(false);
    }
  }, []);

  useEffect(() => {
    connect();
    return () => disconnect();
  }, [connect, disconnect]);

  return { connected, disconnect };
}
