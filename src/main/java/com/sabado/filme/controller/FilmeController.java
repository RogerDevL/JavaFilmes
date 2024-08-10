package com.sabado.filme.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sabado.filme.model.Filme;
import com.sabado.filme.repo.FilmeRepository;

@Controller
@RequestMapping("/filme")
public class FilmeController {
	@Autowired
	private FilmeRepository filmeRepo;
	
	private final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/";
		
	
	
	// http://localhost:8080/filme/
	@GetMapping("/")
	public String inicio(Model model) { // model -> org.springframe..
		model.addAttribute("filmes", filmeRepo.findAll());
		return "index"; //SELECT * FROM filmes;
	}
	
	// http://localhost:8080/filme/form
	@GetMapping("/form")
	public String form(Model model) {
		model.addAttribute("filme", new Filme());
		return "form";
	}
	//http://localhost:8080/filme/form/778 -> id é o 778
	@GetMapping("/form/{id}")
	public String form (@PathVariable("id") Long id, Model model) {
		Optional<Filme> filme = filmeRepo.findById(id);
		if (filme.isPresent()) { // vendo se o filme existe
			model.addAttribute("filme", filme.get()); // se existe, get nele, mostra suas informacoes
		}else {
			model.addAttribute("filme", new Filme()); // caso nao existe, crie um novo filme e volta para o formulario
		}
		return "form"; //se existe o filme, retorna para o formulario para editar
	}
	
	@PostMapping("/add")
	public String addFilme(@RequestParam("id") Optional<Long> id, // gerando id do filme
			
			// passando informacoes dos filmes
			@RequestParam("nome") String nome, 
			@RequestParam("data") String data,
			@RequestParam("imagem") MultipartFile imagem) {
		
		Filme filme; // criando um filme em vazio
		
		if(id.isPresent()) { // verificando se existe o filme com o ID passado
			filme = filmeRepo.findById(id.get()).orElse(new Filme()); // se ele existe, busca o filme pelo id e atualiza suas informaçoes, caso ao contrario crie um filme
		} else {
			filme = new Filme(); // caso ao contrario novamente, criando filme em vazio
		}
		filme.setNome(nome); // atualizando ou criando nome do filme
		filme.setData(Date.valueOf(data)); // atualizando ou criando data 
		
		filmeRepo.save(filme); // salvando o filme
		
		
		// Salvar imagem
		// ! = indica diferente
		if(!imagem.isEmpty()) {
			try {
				// logica para salvar idade
				
				String fileName = "filme" + filme.getId() + "_" + imagem.getOriginalFilename(); 
				Path path = Paths.get(UPLOAD_DIR + fileName); // capturando caminho completo
				Files.write(path, imagem.getBytes()); // escrevendo a imagem
				filme.setImagem("/" +  fileName); // Adicionar o caminho para acessar a imagem
				
				filmeRepo.save(filme); // salvar a imagem
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
			return "redirect:/filme/";
	}
	
	@GetMapping("/delete/{id}")
	public String deleteFilme(@PathVariable("id")Long id) {
		Optional<Filme> filme = filmeRepo.findById(id); // pegando o filme no banco de dadis pelo id
		
		if(filme.isPresent()) { // verifica se existe o filme com o id passado
			Filme filmeParaDeletar = filme.get(); // criando uma variavel de deletar em filme
			String imagePath = UPLOAD_DIR + filmeParaDeletar.getImagem(); // passando caminho da imagem
			try {
				Files.deleteIfExists(Paths.get(imagePath)); // se existir, deletar imagem
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			filmeRepo.deleteById(id); // deletando filme do banco de dados
		}
		return "redirect:/filme/";
	}

}

