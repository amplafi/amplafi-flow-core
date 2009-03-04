/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package org.amplafi.flow.flowproperty;

import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * Test {@link ReflectionFlowPropertyValueProvider}.
 */
public class TestReflectionFlowPropertyValueProvider extends Assert {
    @Test
    public void testGet() {
        User me = new User("me", 9);

        User you = new User("you", 7);
        you.setFriend(me);

        String name = get(me, "name");
        assertEquals(name, "me");

        Integer age = get(me, "age");
        assertEquals(age.intValue(), 9);

        Integer age2 = get(you, "age");
        assertEquals(age2.intValue(), 7);

        assertEquals(get(you, "friend.name"), "me");

        assertNull(get(me, "friend.name"));
    }

    @SuppressWarnings("unchecked")
    private <T> T get(Object object, String... properties) {
        return (T)new ReflectionFlowPropertyValueProvider(object, properties).get(null, null);
    }

    public class User {
        private String name;
        private int age;
        private User friend;

        private User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public User getFriend() {
            return friend;
        }

        public void setFriend(User friend) {
            this.friend = friend;
        }
    }
}
