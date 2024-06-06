package br.com.alura.tabelafipe.principal;

import br.com.alura.tabelafipe.model.Dados;
import br.com.alura.tabelafipe.model.Modelos;
import br.com.alura.tabelafipe.model.Veiculo;
import br.com.alura.tabelafipe.service.ConsumoApi;
import br.com.alura.tabelafipe.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private final Scanner leitura = new Scanner(System.in);
    private static final String URL_BASE = "https://parallelum.com.br/fipe/api/v1/";
    private final ConsumoApi consumo = new ConsumoApi();
    private final ConverteDados conversor = new ConverteDados();

    public void exibirMenu() {
        while (true) {
            System.out.println("""
                ------ MENU DE CONSULTA FIPE ------
                1. Carro
                2. Moto
                3. Caminhão
                4. Sair
                ------------------------------------
                Digite o número da opção desejada:
                """);

            String opcao = leitura.nextLine();
            if (opcao.equals("4")) {
                System.out.println("Obrigado por utilizar o sistema Tabela Fipe. Até logo!");
                break;
            }

            String tipoVeiculo = switch (opcao) {
                case "1" -> "carros";
                case "2" -> "motos";
                case "3" -> "caminhoes";
                default -> null;
            };

            if (tipoVeiculo == null) {
                System.out.println("Opção inválida. Por favor, tente novamente.");
                continue;
            }

            try {
                System.out.println("Consultando marcas de " + tipoVeiculo + "...");
                String endereco = URL_BASE + tipoVeiculo + "/marcas";
                String json = consumo.obterDados(endereco);
                List<Dados> marcas = conversor.obterLista(json, Dados.class);

                System.out.println("------ Marcas Disponíveis ------");
                marcas.stream()
                        .sorted(Comparator.comparing(Dados::codigo))
                        .forEach(marca -> System.out.println(marca.codigo() + ": " + marca.nome()));
                System.out.println("--------------------------------");
                System.out.print("Digite o código da marca desejada: ");
                String codigoMarca = leitura.nextLine();

                endereco = URL_BASE + tipoVeiculo + "/marcas/" + codigoMarca + "/modelos";
                json = consumo.obterDados(endereco);
                Modelos modeloLista = conversor.obterDados(json, Modelos.class);

                System.out.println("\n------ Modelos Disponíveis ------");
                modeloLista.modelos().stream()
                        .sorted(Comparator.comparing(Dados::codigo))
                        .forEach(modelo -> System.out.println(modelo.codigo() + ": " + modelo.nome()));
                System.out.println("---------------------------------");
                System.out.print("Digite um trecho do nome do veículo a ser buscado: ");
                String nomeVeiculo = leitura.nextLine();

                // Adicionando log para verificar o nome do veículo que está sendo buscado
                System.out.println("Buscando modelos que contenham: " + nomeVeiculo);

                List<Dados> modelosFiltrados = modeloLista.modelos().stream()
                        .filter(m -> m.nome().toLowerCase().contains(nomeVeiculo.toLowerCase()))
                        .collect(Collectors.toList());

                // Adicionando log para verificar quantos modelos foram filtrados
                System.out.println("Modelos encontrados: " + modelosFiltrados.size());

                System.out.println("\n------ Modelos Filtrados ------");
                modelosFiltrados.forEach(modelo -> System.out.println(modelo.codigo() + ": " + modelo.nome()));
                System.out.println("-------------------------------");
                System.out.print("Digite o código do modelo para buscar valores de avaliação: ");
                String codigoModelo = leitura.nextLine();

                endereco = URL_BASE + tipoVeiculo + "/marcas/" + codigoMarca + "/modelos/" + codigoModelo + "/anos";
                json = consumo.obterDados(endereco);
                List<Dados> anos = conversor.obterLista(json, Dados.class);
                List<Veiculo> veiculos = new ArrayList<>();

                for (Dados ano : anos) {
                    String enderecoAno = endereco + "/" + ano.codigo();
                    json = consumo.obterDados(enderecoAno);
                    Veiculo veiculo = conversor.obterDados(json, Veiculo.class);
                    veiculos.add(veiculo);
                }

                System.out.println("\n------ Veículos com Avaliações por Ano ------");
                veiculos.forEach(System.out::println);
                System.out.println("--------------------------------------------");

            } catch (Exception e) {
                System.err.println("Erro ao processar a solicitação: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
