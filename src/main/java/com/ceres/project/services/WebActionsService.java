package com.ceres.project.services;

import com.alibaba.fastjson2.JSONObject;
import com.ceres.project.services.appointments.Appointments;
import com.ceres.project.services.appointments.Doctors;
import com.ceres.project.services.auth.AuthService;
import com.ceres.project.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebActionsService {

    private final AuthService authService;
    private final Doctors doctors;
    private final Appointments appointments;

    public OperationReturnObject processAction(String service, String action, JSONObject payload) {
        return switch (service) {
            case "Auth" -> authService.process(action, payload);
            case "doc" -> doctors.process(action, payload);
            case "appointment" -> appointments.process(action, payload);
            default -> {
                OperationReturnObject res = new OperationReturnObject();
                res.setReturnCodeAndReturnMessage(404, "UNKNOWN SERVICE");
                yield res;
            }
        };
    }
}
