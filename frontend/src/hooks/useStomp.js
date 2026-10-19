import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_URL, TOKEN_KEY } from '../utils/constants';

// Live updates. Pass one topic or a list, for example '/topic/board'.
// Pass null to stay disconnected. onMessage(body, topic) is called for every message.
export default function useStomp(topics, onMessage) {
  const handler = useRef(onMessage);
  handler.current = onMessage;

  const key = (Array.isArray(topics) ? topics : [topics]).filter(Boolean).join('|');

  useEffect(() => {
    if (!key) return undefined;
    const list = key.split('|');

    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_URL}/ws`),
      connectHeaders: localStorage.getItem(TOKEN_KEY)
        ? { Authorization: `Bearer ${localStorage.getItem(TOKEN_KEY)}` }
        : {},
      reconnectDelay: 5000,
      onConnect: () => {
        list.forEach((topic) =>
          client.subscribe(topic, (msg) => {
            let body = msg.body;
            try {
              body = JSON.parse(msg.body);
            } catch {
              /* plain text message */
            }
            handler.current?.(body, topic);
          })
        );
      },
    });

    client.activate();
    return () => {
      client.deactivate();
    };
  }, [key]);
}
