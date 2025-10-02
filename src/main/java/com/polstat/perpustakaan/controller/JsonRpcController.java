package com.polstat.perpustakaan.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.polstat.perpustakaan.dto.BookDto;
import com.polstat.perpustakaan.entity.Book;
import com.polstat.perpustakaan.entity.Member;
import com.polstat.perpustakaan.entity.Pinjam;
import com.polstat.perpustakaan.entity.Pinjam.StatusPinjam;
import com.polstat.perpustakaan.repository.BookRepository;
import com.polstat.perpustakaan.repository.MemberRepository;
import com.polstat.perpustakaan.repository.PinjamRepository;
import com.polstat.perpustakaan.rpc.JsonRpcRequest;
import com.polstat.perpustakaan.rpc.JsonRpcResponse;
import com.polstat.perpustakaan.service.BookService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsonRpcController {
    @Autowired
    private BookService bookService;

    @Autowired
    private PinjamRepository pinjamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @PostMapping("/jsonrpc")
    public ResponseEntity<Object> handleJsonRpcRequest(@RequestBody JsonRpcRequest request) {
        try {
            String method = request.getMethod();
            JsonNode params = request.getParams();
            System.out.println("Method: " + method);

            switch (method) {
                case "createBook":
                    String title = params.get("title").asText();
                    String author = params.get("author").asText();
                    String description = params.get("description").asText();

                    BookDto book = BookDto.builder()
                            .title(title)
                            .description(description)
                            .author(author)
                            .build();
                    bookService.createBook(book);

                    return ResponseEntity.ok(new JsonRpcResponse("created", request.getId()));

                case "getBooks":
                    List<BookDto> books = bookService.getBooks();
                    return ResponseEntity.ok(new JsonRpcResponse(books, request.getId()));

                case "pinjamBuku":
                    Long memberId = params.get("memberId").asLong();
                    Long bookId = params.get("bookId").asLong();

                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("Member tidak ditemukan."));
                    Book buku = bookRepository.findById(bookId)
                            .orElseThrow(() -> new RuntimeException("Buku tidak ditemukan."));

                    LocalDate loanDate = LocalDate.now();
                    LocalDate dueDate = loanDate.plusDays(7); // Contoh: jatuh tempo 7 hari

                    Pinjam newLoan = Pinjam.builder()
                            .member(member)
                            .book(buku)
                            .tanggalPinjam(loanDate)
                            .tanggalKembali(dueDate)
                            .status(StatusPinjam.DIPINJAM)
                            .overdueDays(0)
                            .build();
                    pinjamRepository.save(newLoan);
                    return ResponseEntity.ok(new JsonRpcResponse(newLoan, request.getId()));

                case "kembalikanBuku":
                    Long loanId = params.get("loanId").asLong();
                    Pinjam loan = pinjamRepository.findById(loanId)
                            .orElseThrow(() -> new RuntimeException("Peminjaman tidak ditemukan."));

                    if (loan.getStatus() == StatusPinjam.DIKEMBALIKAN) {
                        return ResponseEntity.badRequest().body(new JsonRpcResponse("Buku sudah dikembalikan.", request.getId(), true));
                    }

                    LocalDate returnDate = LocalDate.now();
                    loan.setTanggalDikembalikan(returnDate);
                    loan.setStatus(StatusPinjam.DIKEMBALIKAN);

                    long overdueDays = ChronoUnit.DAYS.between(loan.getTanggalKembali(), returnDate);
                    loan.setOverdueDays((int) Math.max(0, overdueDays));

                    pinjamRepository.save(loan);
                    return ResponseEntity.ok(new JsonRpcResponse(loan, request.getId()));

                case "getAllLoans":
                    List<Pinjam> loans = pinjamRepository.findAll();
                    return ResponseEntity.ok(new JsonRpcResponse(loans, request.getId()));

                default:
                    return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}