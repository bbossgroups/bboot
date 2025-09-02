package org.frameworkset.boot.socket;
/**
 * Copyright 2025 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author biaoping.yin
 * @Date 2025/9/2
 */
public class DeQuotingStringIterator implements Iterator<String> {
    private final String input;
    private final String delims;
    private StringBuilder token;
    private boolean hasToken = false;
    private int i = 0;
    private static final char[] escapes = new char[32];
    static {
        Arrays.fill(escapes, '\uffff');
        escapes[8] = 'b';
        escapes[9] = 't';
        escapes[10] = 'n';
        escapes[12] = 'f';
        escapes[13] = 'r';
    }


    public static String dequote(String str) {
        char start = str.charAt(0);
        if (start == '\'' || start == '"') {
            char end = str.charAt(str.length() - 1);
            if (start == end) {
                return str.substring(1, str.length() - 1);
            }
        }

        return str;
    }
    public DeQuotingStringIterator(String input, String delims) {
        this.input = input;
        this.delims = delims;
        int len = input.length();
        this.token = new StringBuilder(len > 1024 ? 512 : len / 2);
    }

    private void appendToken(char c) {
        if (this.hasToken) {
            this.token.append(c);
        } else {
            if (Character.isWhitespace(c)) {
                return;
            }

            this.token.append(c);
            this.hasToken = true;
        }

    }

    public boolean hasNext() {
        if (this.hasToken) {
            return true;
        } else {
            State state = DeQuotingStringIterator.State.START;
            boolean escape = false;
            int inputLen = this.input.length();

            while(this.i < inputLen) {
                char c = this.input.charAt(this.i++);
                switch (state) {
                    case START:
                        if (c == '\'') {
                            state = DeQuotingStringIterator.State.QUOTE_SINGLE;
                            this.appendToken(c);
                        } else if (c == '"') {
                            state = DeQuotingStringIterator.State.QUOTE_DOUBLE;
                            this.appendToken(c);
                        } else {
                            this.appendToken(c);
                            state = DeQuotingStringIterator.State.TOKEN;
                        }
                        break;
                    case TOKEN:
                        if (this.delims.indexOf(c) >= 0) {
                            return this.hasToken;
                        }

                        if (c == '\'') {
                            state = DeQuotingStringIterator.State.QUOTE_SINGLE;
                        } else if (c == '"') {
                            state = DeQuotingStringIterator.State.QUOTE_DOUBLE;
                        }

                        this.appendToken(c);
                        break;
                    case QUOTE_SINGLE:
                        if (escape) {
                            escape = false;
                            this.appendToken(c);
                        } else if (c == '\'') {
                            this.appendToken(c);
                            state = DeQuotingStringIterator.State.TOKEN;
                        } else if (c == '\\') {
                            escape = true;
                        } else {
                            this.appendToken(c);
                        }
                        break;
                    case QUOTE_DOUBLE:
                        if (escape) {
                            escape = false;
                            this.appendToken(c);
                        } else if (c == '"') {
                            this.appendToken(c);
                            state = DeQuotingStringIterator.State.TOKEN;
                        } else if (c == '\\') {
                            escape = true;
                        } else {
                            this.appendToken(c);
                        }
                }
            }

            return this.hasToken;
        }
    }

    public String next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        } else {
            String ret = this.token.toString();
            this.token.setLength(0);
            this.hasToken = false;
            return dequote(ret.trim());
        }
    }



    public void remove() {
        throw new UnsupportedOperationException("Remove not supported with this iterator");
    }

    private static enum State {
        START,
        TOKEN,
        QUOTE_SINGLE,
        QUOTE_DOUBLE;

        private State() {
        }
    }

}
