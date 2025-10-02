package com.polstat.perpustakaan.dto;

import com.polstat.perpustakaan.entity.Pinjam.StatusPinjam;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinjamDto {
    private Long id;

    @NotNull(message = "ID Member wajib diisi")
    private Long memberId;

    @NotNull(message = "ID Buku wajib diisi")
    private Long bookId;

    private String memberName;
    private String bookTitle;

    @NotNull(message = "Tanggal pinjam wajib diisi")
    private LocalDate tanggalPinjam;

    @NotNull(message = "Tanggal kembali wajib diisi")
    private LocalDate tanggalKembali;

    private LocalDate tanggalDikembalikan;

    @NotNull(message = "Status wajib diisi")
    private StatusPinjam status;

    private Long hariTelat;
}