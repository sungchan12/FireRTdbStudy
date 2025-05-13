package com.example.FirebaseStudy.service;

import com.example.FirebaseStudy.model.User;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseService {

    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference usersRef;

    public FirebaseService(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
        this.usersRef = firebaseDatabase.getReference("users");
    }

    // CREATE: 사용자 추가
    public CompletableFuture<String> saveUser(User user) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // ID가 없으면 새로 생성
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(usersRef.push().getKey());
        }

        // 사용자 데이터 저장
        usersRef.child(user.getId()).setValue(user.toMap(), (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(user.getId());
            }
        });

        return future;
    }

    // READ: 모든 사용자 조회
    public CompletableFuture<List<User>> getAllUsers() {
        CompletableFuture<List<User>> future = new CompletableFuture<>();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> userData = (Map<String, Object>) snapshot.getValue();
                    if (userData != null) {
                        User user = new User();
                        user.setId(snapshot.getKey());
                        user.setName((String) userData.get("name"));
                        user.setEmail((String) userData.get("email"));

                        // Long을 Integer로 변환 (Firebase는 숫자를 Long으로 저장)
                        if (userData.get("age") != null) {
                            Long ageLong = (Long) userData.get("age");
                            user.setAge(ageLong.intValue());
                        }

                        users.add(user);
                    }
                }

                future.complete(users);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        return future;
    }

    // READ: 특정 사용자 조회
    public CompletableFuture<User> getUserById(String userId) {
        CompletableFuture<User> future = new CompletableFuture<>();

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    future.complete(null);
                    return;
                }

                Map<String, Object> userData = (Map<String, Object>) dataSnapshot.getValue();
                if (userData != null) {
                    User user = new User();
                    user.setId(dataSnapshot.getKey());
                    user.setName((String) userData.get("name"));
                    user.setEmail((String) userData.get("email"));

                    if (userData.get("age") != null) {
                        Long ageLong = (Long) userData.get("age");
                        user.setAge(ageLong.intValue());
                    }

                    future.complete(user);
                } else {
                    future.complete(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    // UPDATE: 사용자 정보 업데이트
    public CompletableFuture<Boolean> updateUser(User user) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (user.getId() == null || user.getId().isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("User ID cannot be null or empty"));
            return future;
        }

        // 업데이트할 필드만 선택
        Map<String, Object> updates = new HashMap<>();
        if (user.getName() != null) updates.put("name", user.getName());
        if (user.getEmail() != null) updates.put("email", user.getEmail());
        if (user.getAge() > 0) updates.put("age", user.getAge());

        usersRef.child(user.getId()).updateChildren(updates, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(true);
            }
        });

        return future;
    }

    // DELETE: 사용자 삭제
    public CompletableFuture<Boolean> deleteUser(String userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        usersRef.child(userId).removeValue((error, ref) -> {
            if (error != null) {
                future.completeExceptionally(error.toException());
            } else {
                future.complete(true);
            }
        });

        return future;
    }
}