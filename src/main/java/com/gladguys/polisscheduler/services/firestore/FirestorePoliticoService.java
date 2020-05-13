package com.gladguys.polisscheduler.services.firestore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.gladguys.polisscheduler.model.Partido;
import com.gladguys.polisscheduler.model.Politico;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;

import org.springframework.stereotype.Service;

@Service
public class FirestorePoliticoService {

    private final Firestore db;

    public FirestorePoliticoService(Firestore firestore) {
        this.db = firestore;
    }

    public void addPolitico(Politico politico) {
        db.collection("politicos").document(politico.getId()).set(politico);
    }

    public List<Politico> getPoliticos() throws InterruptedException, ExecutionException {
        List<Politico> politicos = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = db.collection("politicos").get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (DocumentSnapshot document : documents) {
            politicos.add(document.toObject(Politico.class));
        }
        return politicos;
    }

    public void salvarPartidos(List<Partido> partidos) {
        partidos.forEach(p -> {
            db.collection("partidos").document(p.getId()).set(p);
        });
    }

    public Politico getPoliticoById(String id) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> future = db.collection("politicos").document(id).get();
        return future.get().toObject(Politico.class);
    }

    public void limparTotalizadorProposicoesPoliticos() {
        zeradorCampoEmDocumentoPolitico("totalProposicoes");
    }

    public void zerarTotalizadorDespesas() {
        zeradorCampoEmDocumentoPolitico("totalDespesas");
    }

    private void zeradorCampoEmDocumentoPolitico(String campo) {
        QuerySnapshot politicos = null;
        try {
            politicos = db.collection("politicos").get().get();
            politicos.forEach(p -> zeraTotalizador(p.getId(), campo));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void zeraTotalizador(String id, String nomeCampo) {
        db.collection("politicos").document(id).update(campoZeradoMap(nomeCampo));
    }

    private Map<String, Object> campoZeradoMap(String campo) {
        var propZeradaMap = new HashMap<String, Object>();
        propZeradaMap.put(campo, 0);
        return propZeradaMap;
    }

    public void atualizarTotalizadorDespesaPolitico(String politicoId, BigDecimal valorAIncrementar) {
        db.collection("politicos")
                .document(politicoId)
                .update("totalDespesas", FieldValue.increment(valorAIncrementar.doubleValue()));
    }
}