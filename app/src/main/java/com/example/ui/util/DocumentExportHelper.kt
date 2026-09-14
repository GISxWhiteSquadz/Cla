package com.example.ui.util

import android.content.Context
import android.content.Intent
import com.example.data.local.OfficeDocumentEntity
import com.example.model.OfficeLineItem
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentExportHelper {

    fun parseLineItems(json: String): List<OfficeLineItem> {
        if (json.isBlank() || json == "[]") return emptyList()
        val list = mutableListOf<OfficeLineItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    OfficeLineItem(
                        description = obj.optString("description", ""),
                        partNumber = obj.optString("partNumber", ""),
                        quantity = obj.optInt("quantity", 1),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        isLabor = obj.optBoolean("isLabor", false)
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    fun serializeLineItems(items: List<OfficeLineItem>): String {
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("description", item.description)
                put("partNumber", item.partNumber)
                put("quantity", item.quantity)
                put("unitPrice", item.unitPrice)
                put("isLabor", item.isLabor)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun formatDocumentAsPrintable(doc: OfficeDocumentEntity): String {
        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(doc.createdTimestamp))
        val lineItems = parseLineItems(doc.lineItemsJson)

        val sb = StringBuilder()
        sb.appendLine("================================================================")
        sb.appendLine("                 ${doc.docType.uppercase(Locale.GERMANY)}: ${doc.documentNumber}")
        sb.appendLine("================================================================")
        sb.appendLine("Titel:      ${doc.title}")
        sb.appendLine("Datum:      $dateStr")
        sb.appendLine("Status:     ${doc.status}")
        if (doc.customerName.isNotBlank()) {
            sb.appendLine("Kunde:      ${doc.customerName}")
        }
        if (doc.customerContact.isNotBlank()) {
            sb.appendLine("Kontakt:    ${doc.customerContact}")
        }
        if (doc.deviceOrProject.isNotBlank()) {
            sb.appendLine("Gerät/Ref:  ${doc.deviceOrProject}")
        }
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine()
        if (doc.content.isNotBlank()) {
            sb.appendLine(doc.content)
            sb.appendLine()
            sb.appendLine("----------------------------------------------------------------")
        }

        if (lineItems.isNotEmpty()) {
            sb.appendLine("POS | BEZEICHNUNG                       | MENGE | EINZEL | GESAMT")
            sb.appendLine("----+-----------------------------------+-------+--------+-------")
            lineItems.forEachIndexed { index, item ->
                val typeTag = if (item.isLabor) "[Arbeit]" else "[Teil]"
                val desc = "${item.description} $typeTag".take(33).padEnd(33)
                val qty = item.quantity.toString().padStart(5)
                val price = String.format(Locale.GERMANY, "%6.2f €", item.unitPrice)
                val total = String.format(Locale.GERMANY, "%6.2f €", item.total)
                val pos = (index + 1).toString().padStart(3)
                sb.appendLine("$pos | $desc | $qty | $price | $total")
            }
            sb.appendLine("----------------------------------------------------------------")
            val subtotalStr = String.format(Locale.GERMANY, "%.2f €", doc.subtotalEuro)
            val taxStr = String.format(Locale.GERMANY, "%.2f €", doc.subtotalEuro * (doc.taxRatePercent / 100.0))
            val totalStr = String.format(Locale.GERMANY, "%.2f €", doc.totalEuro)

            sb.appendLine("Zwischensumme (Netto):                   $subtotalStr")
            sb.appendLine("Umsatzsteuer (${doc.taxRatePercent}%):                     $taxStr")
            sb.appendLine("GESAMTBETRAG (Brutto):                   $totalStr")
            if (doc.budgetTargetEuro > 0) {
                val budgetStr = String.format(Locale.GERMANY, "%.2f €", doc.budgetTargetEuro)
                val diffStr = String.format(Locale.GERMANY, "%+.2f €", doc.budgetTargetEuro - doc.totalEuro)
                sb.appendLine("Ziel-Budget:                             $budgetStr (Differenz: $diffStr)")
            }
            sb.appendLine("================================================================")
        }

        return sb.toString()
    }

    fun formatDocumentAsHtml(doc: OfficeDocumentEntity): String {
        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(doc.createdTimestamp))
        val lineItems = parseLineItems(doc.lineItemsJson)
        val subtotalStr = String.format(Locale.GERMANY, "%.2f €", doc.subtotalEuro)
        val taxStr = String.format(Locale.GERMANY, "%.2f €", doc.subtotalEuro * (doc.taxRatePercent / 100.0))
        val totalStr = String.format(Locale.GERMANY, "%.2f €", doc.totalEuro)

        val itemsHtml = StringBuilder()
        lineItems.forEachIndexed { index, item ->
            val typeTag = if (item.isLabor) "<span style='color:#0284c7;font-size:10px;font-weight:bold;'>[ARBEIT]</span>" else "<span style='color:#16a34a;font-size:10px;font-weight:bold;'>[TEIL]</span>"
            itemsHtml.append("""
                <tr>
                    <td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:center;">${index + 1}</td>
                    <td style="padding:8px;border-bottom:1px solid #e2e8f0;"><strong>${item.description}</strong> $typeTag ${if (item.partNumber.isNotBlank()) "<br><small style='color:#64748b;'>Art.-Nr: ${item.partNumber}</small>" else ""}</td>
                    <td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:center;">${item.quantity}</td>
                    <td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;">${String.format(Locale.GERMANY, "%.2f €", item.unitPrice)}</td>
                    <td style="padding:8px;border-bottom:1px solid #e2e8f0;text-align:right;font-weight:bold;">${String.format(Locale.GERMANY, "%.2f €", item.total)}</td>
                </tr>
            """.trimIndent())
        }

        val budgetRow = if (doc.budgetTargetEuro > 0) {
            val budgetStr = String.format(Locale.GERMANY, "%.2f €", doc.budgetTargetEuro)
            val diffStr = String.format(Locale.GERMANY, "%+.2f €", doc.budgetTargetEuro - doc.totalEuro)
            "<tr><td colspan='4' style='padding:6px;text-align:right;'>Ziel-Budget / Limit:</td><td style='padding:6px;text-align:right;'><strong>$budgetStr</strong> ($diffStr)</td></tr>"
        } else ""

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>${doc.docType} - ${doc.documentNumber}</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; color: #0f172a; margin: 30px; line-height: 1.5; font-size: 13px; }
                    .header { border-bottom: 3px solid #0284c7; padding-bottom: 14px; margin-bottom: 24px; display: flex; justify-content: space-between; }
                    .title { font-size: 24px; font-weight: bold; color: #0284c7; margin: 0; }
                    .meta { color: #64748b; font-size: 12px; margin-top: 4px; }
                    .box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px; margin-bottom: 20px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 13px; }
                    th { background: #f1f5f9; padding: 10px 8px; text-align: left; border-bottom: 2px solid #cbd5e1; font-weight: 600; }
                    .total-box { margin-top: 20px; width: 340px; margin-left: auto; border: 1px solid #cbd5e1; border-radius: 8px; background: #f8fafc; padding: 12px; }
                    .content-text { white-space: pre-wrap; font-family: inherit; background: #fff; padding: 14px; border-radius: 6px; border: 1px solid #e2e8f0; margin-bottom: 20px; }
                    .footer { margin-top: 40px; padding-top: 12px; border-top: 1px solid #e2e8f0; color: #94a3b8; font-size: 11px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <h1 class="title">${doc.docType.uppercase(Locale.GERMANY)}</h1>
                        <div class="meta">Dokument-Nr: <strong>${doc.documentNumber}</strong> | Datum: $dateStr | Status: ${doc.status}</div>
                    </div>
                </div>

                <div class="box">
                    <div style="font-weight:bold; font-size:14px; color:#0f172a; margin-bottom:4px;">${doc.title}</div>
                    ${if (doc.customerName.isNotBlank()) "<div>Kunde / Empfänger: <strong>${doc.customerName}</strong></div>" else ""}
                    ${if (doc.customerContact.isNotBlank()) "<div>Kontakt: ${doc.customerContact}</div>" else ""}
                    ${if (doc.deviceOrProject.isNotBlank()) "<div>Gerät / Referenz: <strong>${doc.deviceOrProject}</strong></div>" else ""}
                </div>

                ${if (doc.content.isNotBlank()) """
                    <div style="font-weight:bold; margin-bottom:6px; color:#334155;">Beschreibung & Dokumentation:</div>
                    <div class="content-text">${doc.content}</div>
                """.trimIndent() else ""}

                ${if (lineItems.isNotEmpty()) """
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                            <tr>
                                <th style="width:40px;text-align:center;">Pos</th>
                                <th>Bezeichnung / Artikel</th>
                                <th style="width:60px;text-align:center;">Menge</th>
                                <th style="width:90px;text-align:right;">Einzelpreis</th>
                                <th style="width:90px;text-align:right;">Gesamt</th>
                            </tr>
                        </thead>
                        <tbody>
                            $itemsHtml
                        </tbody>
                    </table>

                    <div class="total-box">
                        <table style="margin:0;">
                            <tr><td style="padding:4px;">Zwischensumme:</td><td style="padding:4px;text-align:right;">$subtotalStr</td></tr>
                            <tr><td style="padding:4px;">USt. (${doc.taxRatePercent}%):</td><td style="padding:4px;text-align:right;">$taxStr</td></tr>
                            <tr style="border-top:2px solid #0284c7; font-size:15px; font-weight:bold; color:#0284c7;">
                                <td style="padding:8px 4px 4px 4px;">Gesamtbetrag:</td><td style="padding:8px 4px 4px 4px;text-align:right;">$totalStr</td>
                            </tr>
                            $budgetRow
                        </table>
                    </div>
                """.trimIndent() else ""}

                <div class="footer">
                    Erstellt mit CoreRepair Office &bull; Dokument-ID: ${doc.documentNumber} &bull; Gültig ohne eigenhändige Unterschrift
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun printDocumentAsPdf(context: Context, doc: OfficeDocumentEntity) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager ?: return
        val jobName = "${doc.docType}_${doc.documentNumber}"
        val htmlContent = formatDocumentAsHtml(doc)

        val webView = android.webkit.WebView(context)
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    fun shareDocument(context: Context, doc: OfficeDocumentEntity) {
        val printableText = formatDocumentAsPrintable(doc)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "${doc.docType}: ${doc.documentNumber} - ${doc.title}")
            putExtra(Intent.EXTRA_SUBJECT, "${doc.docType} ${doc.documentNumber}")
            putExtra(Intent.EXTRA_TEXT, printableText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Dokument exportieren / versenden")
        context.startActivity(shareIntent)
    }
}
