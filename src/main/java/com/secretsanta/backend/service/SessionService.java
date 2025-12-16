package com.secretsanta.backend.service;

import com.secretsanta.backend.model.SessionDocument;
import com.secretsanta.backend.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    // CREATE SESSION
    public SessionDocument createSession(List<String> members) {

        List<String> cleaned = members.stream()
                .map(m -> m.trim().toLowerCase())
                .toList();

        if (new HashSet<>(cleaned).size() != cleaned.size())
            throw new RuntimeException("Duplicate names not allowed");

        SessionDocument session = new SessionDocument();
        session.setId(UUID.randomUUID().toString().substring(0, 6).toLowerCase());
        session.setMembers(new ArrayList<>(cleaned));
        session.setLocked(false);
        session.setCreatedAt(LocalDateTime.now());
        session.setDeviceBindings(new HashMap<>());

        sessionRepository.save(session);
        return session;
    }

    // JOIN SESSION (DEVICE BIND)
//    public void joinSession(String sessionId, String name, String deviceId) {

//     sessionId = sessionId.toLowerCase();
//     name = name.trim().toLowerCase();

//     SessionDocument session = sessionRepository.findById(sessionId)
//             .orElseThrow(() -> new RuntimeException("Session not found"));

//     if (!session.getMembers().contains(name))
//         throw new RuntimeException("You are not part of this session");

//     if (session.getDeviceBindings() == null)
//         session.setDeviceBindings(new HashMap<>());

//     // 🔒 device already used → block reuse
//     if (session.getDeviceBindings().containsKey(deviceId))
//         throw new RuntimeException("This device has already joined");

//     // 🔒 name already claimed by another device
//     if (session.getDeviceBindings().containsValue(name))
//         throw new RuntimeException("This name is already taken");

//     session.getDeviceBindings().put(deviceId, name);
//     sessionRepository.save(session);
// }

    public String joinSession(String sessionId, String name, String deviceId) {

    sessionId = sessionId.toLowerCase();
    name = name.trim().toLowerCase();

    SessionDocument session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));

    if (!session.getMembers().contains(name))
        throw new RuntimeException("You are not part of this session");

    if (session.getDeviceBindings() == null)
        session.setDeviceBindings(new HashMap<>());

    if (session.getAccessTokens() == null)
        session.setAccessTokens(new HashMap<>());

    // 🔒 name already used
    if (session.getAccessTokens().containsKey(name))
        throw new RuntimeException("This name has already viewed assignment");

    // 🔒 device already used
    if (session.getDeviceBindings().containsKey(deviceId))
        throw new RuntimeException("This device already joined");

    session.getDeviceBindings().put(deviceId, name);

    // 🎟️ ISSUE ONE-TIME TOKEN
    String token = UUID.randomUUID().toString();
    session.getAccessTokens().put(name, token);

    sessionRepository.save(session);
    return token;
}



    // GET ASSIGNMENT
    // public Map<String, String> getAssignment(String sessionId, String deviceId) {

    //     SessionDocument session = sessionRepository.findById(sessionId)
    //             .orElseThrow(() -> new RuntimeException("Session not found"));

    //     String name = session.getDeviceBindings().get(deviceId);
    //     if (name == null)
    //         throw new RuntimeException("Unauthorized device");

    //     if (!session.isLocked()) {
    //         session.setAssignedTargets(generateAssignments(session.getMembers()));
    //         session.setLocked(true);
    //         sessionRepository.save(session);
    //     }

    //     return Map.of("target", session.getAssignedTargets().get(name));
    // }

    public Map<String, String> getAssignment(String sessionId,String deviceId,String token) {

    SessionDocument session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));

    String name = session.getDeviceBindings().get(deviceId);
    if (name == null)
        throw new RuntimeException("Unauthorized device");

    String savedToken = session.getAccessTokens().get(name);
    if (savedToken == null || !savedToken.equals(token))
        throw new RuntimeException("Assignment already viewed or invalid token");

    if (!session.isLocked()) {
        session.setAssignedTargets(generateAssignments(session.getMembers()));
        session.setLocked(true);
    }

    // 🔥 CONSUME TOKEN (cannot view again)
    session.getAccessTokens().remove(name);
    sessionRepository.save(session);

    return Map.of("target", session.getAssignedTargets().get(name));
}


//     private Map<String, String> generateAssignments(List<String> members) {

//     Map<String, String> assigned = new HashMap<>();
//     List<String> remaining = new ArrayList<>(members);

//     // 🎯 SPECIAL PAIRS (FORCED)
//     forcePair(assigned, remaining, "vaishali", "paras");
//     forcePair(assigned, remaining, "aanchal", "devang");

//     // 🎲 RANDOM ASSIGNMENT FOR REST
//     Collections.shuffle(remaining);
//     List<String> receivers = new ArrayList<>(remaining);
//     Collections.shuffle(receivers);

//     for (int i = 0; i < remaining.size(); i++) {
//         if (remaining.get(i).equals(receivers.get(i))) {
//             // reshuffle if self-assigned
//             Collections.shuffle(receivers);
//             i = -1;
//             continue;
//         }
//         assigned.put(remaining.get(i), receivers.get(i));
//     }

//     return assigned;
// }


//     private void forcePair(
//         Map<String, String> assigned,
//         List<String> remaining,
//         String a,
//         String b
// ) {
//     if (remaining.contains(a) && remaining.contains(b)) {
//         assigned.put(a, b);
//         assigned.put(b, a);
//         remaining.remove(a);
//         remaining.remove(b);
//     }
// }

private Map<String, String> generateAssignments(List<String> members) {

    Map<String, String> assigned = new HashMap<>();
    List<String> senders = new ArrayList<>(members);
    List<String> receivers = new ArrayList<>(members);

    // 🎯 ONE-WAY FORCED PAIR
    if (senders.contains("vaishali") && receivers.contains("paras")) {
        assigned.put("vaishali", "paras");
        senders.remove("vaishali");   // vaishali already assigned
        receivers.remove("paras");    // paras cannot receive again
    }

    // 🎲 RANDOM ASSIGNMENT FOR REST
    Collections.shuffle(senders);
    Collections.shuffle(receivers);

    for (int i = 0; i < senders.size(); i++) {
        if (senders.get(i).equals(receivers.get(i))) {
            Collections.shuffle(receivers);
            i = -1;
            continue;
        }
        assigned.put(senders.get(i), receivers.get(i));
    }

    return assigned;
}


//handing admin 
    public Map<String, Object> getAdminStats(String sessionId) {

    SessionDocument session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));

    Map<String, Object> stats = new HashMap<>();
    stats.put("sessionId", session.getId());
    stats.put("totalMembers", session.getMembers().size());
    stats.put("joinedCount",
            session.getDeviceBindings() == null ? 0 : session.getDeviceBindings().size());
    stats.put("locked", session.isLocked());
    stats.put("createdAt", session.getCreatedAt());
    stats.put("assignmentsGenerated", session.getAssignedTargets() != null);

    return stats;
}

}
